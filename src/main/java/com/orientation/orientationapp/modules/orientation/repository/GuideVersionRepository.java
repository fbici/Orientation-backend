package com.orientation.orientationapp.modules.orientation.repository;

import com.orientation.orientationapp.common.enums.GuideVersionStatus;
import com.orientation.orientationapp.modules.orientation.entity.GuideVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuideVersionRepository extends JpaRepository<GuideVersion, UUID> {
    List<GuideVersion> findByOrientationGuideId(UUID orientationGuideId);
    Optional<GuideVersion> findByOrientationGuideIdAndVersionNumber(UUID orientationGuideId, Integer versionNumber);
    Optional<GuideVersion> findByOrientationGuideIdAndActiveTrue(UUID orientationGuideId);
    List<GuideVersion> findByStatus(GuideVersionStatus status);
}
