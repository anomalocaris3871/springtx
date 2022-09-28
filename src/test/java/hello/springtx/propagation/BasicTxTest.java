package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {
    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("transaction start");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("transaction commit start");

        txManager.commit(status);
        log.info("transaction commit complete");
    }

    @Test
    void rollback() {
        log.info("transaction start");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("transaction rollback start");

        txManager.rollback(status);
        log.info("transaction rollback complete");
    }

    @Test
    void double_commit() {
        log.info("transaction1 start");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("transaction1 commit start");
        txManager.commit(tx1);

        log.info("transaction2 start");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("transaction2 commit start");
        txManager.commit(tx2);

    }

    @Test
    void double_commit_rollback() {
        log.info("transaction1 start");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("transaction1 commit start");
        txManager.commit(tx1);

        log.info("transaction2 start");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("transaction2 rollback start");
        txManager.rollback(tx2);

    }

    @Test
    void inner_commit() {
        log.info("outer transaction start");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

        inner();

        log.info("outer transaction commit");
        txManager.commit(outer);
    }

    private void inner() {
        log.info("inner transaction start");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}",inner.isNewTransaction());
        log.info("inner transaction commit");
        txManager.commit(inner);
    }

    @Test
    void outer_rollback() {
        log.info("outer transaction start");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());


        inner();

        log.info("outer transaction rollback");
        txManager.rollback(outer);
    }

    @Test
    void inner_rollback() {
        log.info("outer transaction start");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());


        log.info("inner transaction start");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner transaction rollback");
        txManager.rollback(inner);

        log.info("outer transaction commit");
        Assertions.assertThatThrownBy(()-> txManager.commit(outer)).isInstanceOf(UnexpectedRollbackException.class);
    }

}
