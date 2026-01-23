package File.Node.storage.repository;

import File.Node.storage.model.FileMetadata;
import File.Node.storage.model.FileMetadataProjection;
import File.Node.storage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    // Find all files by User entity
    List<FileMetadata> findByUser(User user);

    // Find a single file by its unique file key
    Optional<FileMetadata> findByFileKey(String fileKey);

    @Query("SELECT f.filename AS filename, f.relativePath AS relativePath, f.fileKey AS fileKey, f.uploadedAt AS uploadedAt FROM FileMetadata f WHERE f.fileKey = :fileKey")
    Optional<FileMetadataProjection> findByFileKeyProjected(@Param("fileKey") String fileKey);

}