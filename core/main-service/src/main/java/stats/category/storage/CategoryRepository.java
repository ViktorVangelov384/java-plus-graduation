package stats.category.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import stats.category.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
