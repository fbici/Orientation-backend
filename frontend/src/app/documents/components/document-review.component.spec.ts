import { of, throwError, Observable } from "rxjs";
import { convertToParamMap } from "@angular/router";
import { DocumentReviewComponent } from "./document-review.component";
import {
  AcademicFieldsComponent,
  AcademicField,
} from "../../catalog/academic-fields.component";
describe("F2 human review decisions", () => {
  let api: any;
  let c: DocumentReviewComponent;
  beforeEach(() => {
    api = jasmine.createSpyObj("Api", [
      "reviewDocument",
      "getCountries",
      "reference",
      "reviewFact",
      "publishDocument",
      "documentPage",
    ]);
    api.reviewDocument.and.returnValue(
      of({ title: "Synthetic source", pages: [], facts: [] }),
    );
    api.getCountries.and.returnValue(of([]));
    api.reference.and.returnValue(of([]));
    c = new DocumentReviewComponent(api, {
      snapshot: { paramMap: convertToParamMap({ id: "doc" }) },
    } as any);
    c.ngOnInit();
  });
  it("refuses publication without required guide metadata", () => {
    c.chosen.add("fact");
    c.publish();
    expect(api.publishDocument).not.toHaveBeenCalled();
    expect(c.error()).toContain("Renseignez");
  });
  it("shows rejection without claiming publication succeeded", () => {
    c.publication = {
      countryId: "country",
      academicYearId: "year",
      guideTitle: "Synthetic",
      effectiveDate: "2026-01-01",
      expiryDate: "",
      publisher: "",
    };
    c.chosen.add("fact");
    api.publishDocument.and.returnValue(
      throwError(() => ({ error: { message: "Dependency not validated" } })),
    );
    c.publish();
    expect(c.error()).toBe("Dependency not validated");
    expect(c.message()).toBe("");
    expect(c.busy()).toBeFalse();
  });
  it("submits the opened fact version and leaves errors visible on conflict", () => {
    c.selected = { id: "fact", version: 7 };
    c.values = { name: "Reviewed" };
    api.reviewFact.and.returnValue(
      throwError(() => ({ error: { message: "Reload changed fact" } })),
    );
    c.decide("VALIDATED");
    expect(api.reviewFact).toHaveBeenCalledWith(
      "doc",
      "fact",
      jasmine.objectContaining({ version: 7, status: "VALIDATED" }),
    );
    expect(c.error()).toBe("Reload changed fact");
  });
  it("reloads source state only after a persisted decision", () => {
    c.selected = { id: "fact", version: 1 };
    api.reviewFact.and.returnValue(
      of({ id: "fact", version: 2, status: "REJECTED" }),
    );
    c.decide("REJECTED");
    expect(c.selected.version).toBe(2);
    expect(c.message()).toContain("REJECTED");
  });
});
describe("F2 preview lifecycle", () => {
  it("cancels the old page before requesting another and cancels the last on destroy", () => {
    let cancelled=0;
    const api={documentPage:()=>new Observable<Blob>(()=>()=>cancelled++)};
    const c=new DocumentReviewComponent(api as any,{} as any);
    c.review.set({pages:[{pageNumber:1},{pageNumber:2}]});
    c.preview();c.page=2;c.preview();expect(cancelled).toBe(1);
    c.ngOnDestroy();expect(cancelled).toBe(2);
  });
});
describe("F2 academic references", () => {
  const field: AcademicField = {
    key: "facultyId",
    label: "Faculté",
    ref: "faculties",
  };
  let c: AcademicFieldsComponent;
  beforeEach(() => (c = new AcademicFieldsComponent({} as any)));
  it("makes draft and existing parent references mutually exclusive", () => {
    c.values = { facultyId: "old" };
    c.selectReference(field, "fact:draft");
    expect(c.values["facultyId"]).toBeUndefined();
    expect(c.values["facultyFactId"]).toBe("draft");
    c.selectReference(field, "real");
    expect(c.values["facultyFactId"]).toBeUndefined();
    expect(c.values["facultyId"]).toBe("real");
  });
  it("preserves an explicit optional rule decision as boolean false", () => {
    c.values = {};
    c.setValue({ key: "mandatory", label: "Obligatoire" }, "false");
    expect(c.values["mandatory"]).toBeFalse();
  });

  it("associates an existing university without inventing missing location references", () => {
    c.kind = "UNIVERSITY";
    c.allowExistingTarget = true;
    c.values = { name: "Reviewed source name" };
    c.options["existingEntityId"] = [
      { id: "real", name: "Existing", countryId: "country", cityId: "city" },
    ];
    c.selectReference(
      { key: "existingEntityId", label: "Université", ref: "universities" },
      "real",
    );
    expect(c.values["existingEntityId"]).toBe("real");
    expect(c.values["countryId"]).toBe("country");
    expect(c.values["cityId"]).toBe("city");
    expect(c.values["name"]).toBe("Reviewed source name");
  });
});
