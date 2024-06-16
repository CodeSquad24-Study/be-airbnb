package team07.airbnb.integration.util;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Profile("test")
public class DataBaseCleaner {

    private static final String FOREIGN_KEY_CHECK_FORMAT = "SET FOREIGN_KEY_CHECKS %d";
    private static final String TRUNCATE_FORMAT = "TRUNCATE TABLE %s";

    private final List<String> tableNames = new ArrayList<>();

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @PostConstruct
    public void findDatabaseTableNames() {
        List<String> tableInfos = entityManager.createNativeQuery("SHOW TABLES").getResultList();
        tableNames.addAll(tableInfos);
    }

    @Transactional
    public void clear(String ... withOuts) {
        entityManager.clear();
        Set<String> names = new HashSet<>(Arrays.asList(withOuts));
        names.add("USERS"); // 유저 테이블은 초기화 제외
        truncate(names);
    }

    private void truncate(Set<String> withOut) {
        entityManager.createNativeQuery(String.format(FOREIGN_KEY_CHECK_FORMAT, 0)).executeUpdate(); // FK 제약조건 해제
        for (String tableName : tableNames) {
            if(withOut.contains(tableName)) continue;
            entityManager.createNativeQuery(String.format(TRUNCATE_FORMAT, tableName)).executeUpdate();
        }
        entityManager.createNativeQuery(String.format(FOREIGN_KEY_CHECK_FORMAT, 1)).executeUpdate();
    }
}
