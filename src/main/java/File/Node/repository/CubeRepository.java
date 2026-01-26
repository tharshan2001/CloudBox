package File.Node.repository;

import File.Node.entity.Cube;
import File.Node.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CubeRepository extends JpaRepository<Cube, Long> {
    List<Cube> findByOwner(User owner);

    Optional<Cube> findByApiKey(String apiKey);

    @Query("""
                SELECT c FROM Cube c
                JOIN FETCH c.owner
                WHERE c.apiKey = :apiKey
            """)
    Optional<Cube> findByApiKeyWithOwner(@Param("apiKey") String apiKey);

}
