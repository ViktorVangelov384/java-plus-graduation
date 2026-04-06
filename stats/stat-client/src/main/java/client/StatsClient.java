package client;

import contract.StatsOperation;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "stats-server")
public interface StatsClient extends StatsOperation {
}
