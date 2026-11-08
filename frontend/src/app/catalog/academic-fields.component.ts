import { Component, Input, OnChanges } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ApiService } from "../core/services/api.service";

export interface AcademicField {
  key: string;
  label: string;
  type?: string;
  required?: boolean;
  ref?: string;
  options?: string[];
}
const field = (
  key: string,
  label: string,
  type = "text",
  required = false,
  ref?: string,
  options?: string[],
): AcademicField => ({ key, label, type, required, ref, options });
const name = field("name", "Nom", "text", true);
const country = field(
  "countryId",
  "Pays",
  "select",
  true,
  "locations/countries",
);
const city = field("cityId", "Ville", "select", true, "locations/cities");
const year = field(
  "academicYearId",
  "Année académique",
  "select",
  true,
  "academic-years",
);
const description = field("description", "Description", "textarea");
export const ACADEMIC_FIELDS: Record<string, AcademicField[]> = {
  UNIVERSITY: [
    name,
    field("shortName", "Sigle"),
    country,
    city,
    field("type", "Type d'établissement", "select", false, undefined, [
      "PUBLIC",
      "PRIVATE",
      "OTHER",
    ]),
    field("status", "Statut", "select", false, undefined, [
      "PENDING",
      "ACTIVE",
      "INACTIVE",
      "SUSPENDED",
    ]),
    field("address", "Adresse"),
    field("phone", "Téléphone"),
    field("email", "Courriel", "email"),
    field("website", "Site web", "url"),
    description,
    field("sourceUrl", "URL de la source", "url"),
  ],
  CAMPUS: [
    name,
    field("universityId", "Université", "select", true, "universities"),
    city,
    field("address", "Adresse"),
    field("phone", "Téléphone"),
    field("main", "Campus principal", "checkbox"),
    field("active", "Actif", "checkbox"),
  ],
  FACULTY: [
    name,
    field("campusId", "Campus", "select", true, "campuses"),
    field("code", "Code"),
    description,
    field("active", "Active", "checkbox"),
  ],
  PROGRAM: [
    name,
    field("facultyId", "Faculté", "select", true, "faculties"),
    field("code", "Code"),
    field("type", "Type de diplôme", "select", true, undefined, [
      "LICENSE",
      "LICENSE_PRO",
      "MASTER",
      "MASTER_PRO",
      "ENGINEERING",
      "DOCTORATE",
      "BTS",
      "DUT",
      "OTHER",
    ]),
    field("degree", "Diplôme"),
    field("duration", "Durée en années", "number", true),
    field("language", "Langue"),
    field("studyField", "Domaine"),
    description,
    field("prerequisites", "Prérequis", "textarea"),
    field("tuitionFee", "Frais de scolarité", "number"),
    field("tuitionCurrency", "Devise ISO (ex. XOF)"),
    year,
    field("available", "Formation disponible", "checkbox"),
    field("sourceUrl", "URL de la source", "url"),
  ],
  SCHOLARSHIP: [
    name,
    country,
    year,
    field("type", "Type de bourse", "select", true, undefined, [
      "MERIT",
      "NEED_BASED",
      "GOVERNMENT",
      "INTERNATIONAL",
      "SPORT",
      "RESEARCH",
      "PARTIAL",
      "FULL",
      "TUITION_ONLY",
      "OTHER",
    ]),
    field("status", "Statut", "select", true, undefined, [
      "ACTIVE",
      "CLOSED",
      "EXPIRED",
      "SUSPENDED",
    ]),
    field("provider", "Organisme"),
    description,
    field("coverage", "Couverture"),
    field("amount", "Montant", "number"),
    field("currency", "Devise ISO"),
    field("duration", "Durée", "number"),
    field("deadline", "Date limite", "date"),
    field("validFrom", "Début de validité", "date"),
    field("validUntil", "Fin de validité", "date"),
    field("applicationUrl", "Lien de candidature", "url"),
    field("sourceUrl", "Source", "url"),
    field("universityId", "Université", "select", false, "universities"),
    field("programId", "Formation", "select", false, "programs"),
  ],
  ADMISSION_CRITERION: [
    field("programId", "Formation", "select", true, "programs"),
    field("criterionType", "Critère", "select", true, undefined, [
      "MIN_AVERAGE",
      "MAX_AVERAGE",
      "MIN_GRADE",
      "MAX_GRADE",
      "SPECIFIC_GRADE",
      "CUSTOM",
    ]),
    field("operator", "Opérateur", "select", true, undefined, [
      "EQUAL",
      "NOT_EQUAL",
      "GREATER_THAN",
      "GREATER_THAN_OR_EQUAL",
      "LESS_THAN",
      "LESS_THAN_OR_EQUAL",
      "BETWEEN",
      "IN_LIST",
      "NOT_IN_LIST",
    ]),
    field("subjectId", "Matière et barème", "select", false, "subjects"),
    field("minValue", "Valeur minimale", "number"),
    field("maxValue", "Valeur maximale", "number"),
    field("valueScaleMax", "Barème explicite (ex. 20)", "number"),
    field("stringValue", "Valeurs acceptées / exigées"),
    field("fieldName", "Champ académique", "select", false, undefined, [
      "bacType",
      "requiredDiploma",
      "language",
      "nationality",
      "residence",
      "requiredDocument",
    ]),
    field("logicalGroup", "Groupe logique", "text", true),
    field("logicalOperator", "Logique du groupe", "select", true, undefined, [
      "AND",
      "OR",
    ]),
    field("mandatory", "Critère obligatoire", "select", true, undefined, [
      "true",
      "false",
    ]),
    description,
  ],
};
ACADEMIC_FIELDS["SCHOLARSHIP_CRITERION"] = [
  field("scholarshipId", "Bourse", "select", true, "scholarships"),
  ...ACADEMIC_FIELDS["ADMISSION_CRITERION"].filter(
    (f) => !["programId", "logicalGroup", "logicalOperator"].includes(f.key),
  ),
];
const kinds: Record<string, string> = {
  universities: "UNIVERSITY",
  campuses: "CAMPUS",
  faculties: "FACULTY",
  programs: "PROGRAM",
  scholarships: "SCHOLARSHIP",
};
@Component({
  selector: "app-academic-fields",
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `<div class="g2">
    @for (f of fields; track f.key) {
      <div class="form-group">
        <label class="form-label" [for]="'field-' + f.key"
          >{{ f.label }}{{ f.required ? " *" : "" }}</label
        >
        @if (f.ref) {
          <input
            class="form-input"
            [attr.aria-label]="'Rechercher ' + f.label"
            placeholder="Rechercher une référence…"
            (input)="lookup(f, $any($event.target).value)"
          />
          <select
            class="form-input"
            [id]="'field-' + f.key"
            [ngModel]="referenceValue(f)"
            (ngModelChange)="selectReference(f, $event)"
            [required]="!!f.required"
          >
            <option value="">Non renseigné</option>
            @if (values[f.key] && !hasOption(f, values[f.key])) {
              <option [value]="values[f.key]">
                Référence actuelle : {{ values[f.key] }}
              </option>
            }
            @for (o of options[f.key] || []; track o.id) {
              <option [value]="o.id">
                {{ o.name || o.label }}
                {{ o.academicYearId ? "— " + o.academicYearId : "" }}
              </option>
            }
            @for (d of draftsFor(f); track d.id) {
              <option [value]="'fact:' + d.id">
                Source p. {{ d.pageNumber }} — {{ d.values.name || d.kind }} ({{
                  d.status
                }})
              </option>
            }
          </select>
        } @else if (f.options) {
          <select
            class="form-input"
            [id]="'field-' + f.key"
            [required]="!!f.required"
            [ngModel]="values[f.key]"
            (ngModelChange)="setValue(f, $event)"
          >
            <option [ngValue]="null">Non renseigné</option>
            @for (o of f.options; track o) {
              <option [value]="o">{{ o }}</option>
            }
          </select>
        } @else if (f.type === "textarea") {
          <textarea
            class="form-input"
            [id]="'field-' + f.key"
            [(ngModel)]="values[f.key]"
            [required]="!!f.required"
            rows="3"
          ></textarea>
        } @else {
          <input
            class="form-input"
            [id]="'field-' + f.key"
            [type]="f.type || 'text'"
            [(ngModel)]="values[f.key]"
            [required]="!!f.required"
            [attr.min]="f.type === 'number' ? 0 : null"
            [attr.step]="f.type === 'number' ? 'any' : null"
          />
        }
        @if (errors[f.key]) {
          <small class="text-danger" role="alert">{{ errors[f.key] }}</small>
        }
      </div>
    }
  </div>`,
})
export class AcademicFieldsComponent implements OnChanges {
  @Input() kind = "PROGRAM";
  @Input() values: Record<string, any> = {};
  @Input() drafts: any[] = [];
  @Input() allowExistingTarget = false;
  options: Record<string, any[]> = {};
  errors: Record<string, string> = {};
  constructor(private api: ApiService) {}
  get fields(): AcademicField[] {
    const standard = ACADEMIC_FIELDS[this.kind] || [
      description,
      field("amount", "Montant", "number"),
      field("currency", "Devise"),
      field("date", "Date", "date"),
    ];
    const resource = Object.keys(kinds).find((key) => kinds[key] === this.kind);
    return this.allowExistingTarget && resource
      ? [
          field(
            "existingEntityId",
            "Associer à une fiche existante (facultatif)",
            "select",
            false,
            resource,
          ),
          ...standard,
        ]
      : standard;
  }
  ngOnChanges() {
    for (const f of this.fields) if (f.ref) this.lookup(f, "");
  }
  lookup(f: AcademicField, search: string) {
    this.api.reference(f.ref!, search).subscribe({
      next: (r) => {
        this.options[f.key] = Array.isArray(r) ? r : r.content;
        delete this.errors[f.key];
      },
      error: () =>
        (this.errors[f.key] =
          "Références indisponibles. Réessayer la recherche."),
    });
  }
  draftsFor(f: AcademicField) {
    if (f.key === "existingEntityId") return [];
    return this.drafts.filter(
      (d) => d.kind === kinds[f.ref!] && d.status !== "REJECTED",
    );
  }
  hasOption(f: AcademicField, id: string) {
    return (this.options[f.key] || []).some((o) => o.id === id);
  }
  referenceValue(f: AcademicField) {
    return (
      this.values[f.key] ||
      (this.values[f.key.replace(/Id$/, "FactId")]
        ? "fact:" + this.values[f.key.replace(/Id$/, "FactId")]
        : "")
    );
  }
  selectReference(f: AcademicField, value: string) {
    if (f.key === "existingEntityId" && value && !value.startsWith("fact:")) {
      const record = (this.options[f.key] || []).find(
        (row) => row.id === value,
      );
      if (record) {
        for (const column of ACADEMIC_FIELDS[this.kind] || []) {
          const missing =
            this.values[column.key] === null ||
            this.values[column.key] === undefined ||
            this.values[column.key] === "";
          if (
            missing &&
            !this.values[column.key.replace(/Id$/, "FactId")] &&
            record[column.key] !== undefined
          )
            this.values[column.key] = record[column.key];
        }
      }
    }
    delete this.values[f.key];
    delete this.values[f.key.replace(/Id$/, "FactId")];
    if (value)
      this.values[
        value.startsWith("fact:") ? f.key.replace(/Id$/, "FactId") : f.key
      ] = value.replace(/^fact:/, "");
  }
  setValue(f: AcademicField, value: any) {
    this.values[f.key] = f.key === "mandatory" ? value === "true" : value;
  }
}
