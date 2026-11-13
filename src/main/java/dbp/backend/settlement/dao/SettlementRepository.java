package dbp.backend.settlement.dao;

import dbp.backend.common.config.database.DatabaseConfig;
import dbp.backend.common.exception.ApiException;
import dbp.backend.common.exception.DatabaseException;
import dbp.backend.common.exception.code.CommonErrorCode;
import dbp.backend.settlement.dto.request.CreateSettlementRequest;
import dbp.backend.settlement.dto.response.SettlementSummaryResponse;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

@Repository
public class SettlementRepository {

    private final SqlSessionFactory sqlSessionFactory;

    public SettlementRepository() {
        Properties properties = new Properties();
        properties.setProperty("DB_DRIVER", DatabaseConfig.DB_DRIVER);
        properties.setProperty("DB_URL", DatabaseConfig.DB_URL);
        properties.setProperty("DB_USERNAME", DatabaseConfig.DB_USERNAME);
        properties.setProperty("DB_PASSWORD", DatabaseConfig.DB_PASSWORD);

        try (InputStream inputStream = Resources.getResourceAsStream("mybatis/mybatis-config.xml")) {
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, properties);

        } catch (IOException e) {
            throw new ApiException(CommonErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    public void insertSettlement(Long memberId, CreateSettlementRequest request) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
            SettlementMapper mapper = sqlSession.getMapper(SettlementMapper.class);

            // 트랜잭션 사용
            try {
                // 정산 삽입
                Long settlementId = mapper.nextSettlementId();
                if (mapper.insertSettlement(settlementId, memberId, request) != 1) {
                    throw new DatabaseException();
                }

                // 멤버별 정산 삽입
                for (CreateSettlementRequest.SettlementParticipant participant : request.participants()) {
                    if (mapper.insertSettlementShare(settlementId, request.meetupId(), participant) < 1) {
                        throw new DatabaseException();
                    }
                }

                sqlSession.commit();

            } catch (RuntimeException e) {
                try {
                    sqlSession.rollback();
                } catch (RuntimeException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                throw e;
            }
        } catch (PersistenceException e) {
            throw new DatabaseException(e);
        }
    }

    public List<Long> findMeetupParticipantMemberIds(Long meetupId) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return sqlSession.getMapper(SettlementMapper.class).findParticipantMemberIds(meetupId);
        } catch (PersistenceException e) {
            throw new DatabaseException(e);
        }
    }

    public List<SettlementSummaryResponse> findSettlementsByMeetupId(Long meetupId) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return sqlSession.getMapper(SettlementMapper.class).findSettlementsByMeetupId(meetupId);
        } catch (PersistenceException e) {
            throw new DatabaseException(e);
        }
    }
}
