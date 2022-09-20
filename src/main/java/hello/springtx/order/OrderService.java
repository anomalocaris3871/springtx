package hello.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        log.info("order call");
        orderRepository.save(order);

        log.info("");
        if (order.getUsername().equals("exception")) {
            log.info("System exception occurs");
            throw new RuntimeException("System exception");

        } else if (order.getUsername().equals("shortage")) {
            log.info("Shortage of balance exception occurs");
            order.setPayStatus("Pending");
            throw new NotEnoughMoneyException("The balance is insufficient");
        } else {
            log.info("normal approval");
            order.setPayStatus("Completion");
        }
    }
}
