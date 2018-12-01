package ru.avplatonov.keter.backend.controllers.monitoring.schedule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.avplatonov.keter.backend.initialize.monitoring.schedule.Schedule;

import static ru.avplatonov.keter.backend.db.ScheduleDB.listSchedule;

@RestController
@EnableAutoConfiguration
public class RefreshScheduleState {
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @RequestMapping(value = "/graph/status/state")
    public List<Schedule> scheduleRefresh(
            @RequestParam String ids
    ) throws IOException {

        System.err.print(ids);

        List<Schedule> listRefreshSchedule = new ArrayList<>();
        List<String> listUUID = parsGet(ids);
        for (Schedule schedule : listSchedule) {
            for (String idGraph : listUUID){
                if(schedule.getUuidShedule().toString().equals(idGraph)) {
                    double rand = Math.random();
                    if (rand <= 0.2)
                        schedule.setState("planning");
                    if (rand > 0.2 && rand <= 0.4)
                        schedule.setState("started");
                    if (rand > 0.2 && rand <= 0.4)
                        schedule.setState("failed");
                    if (rand > 0.2 && rand <= 0.4)
                        schedule.setState("succeed");
                    else schedule.setState("canceled");
                    listRefreshSchedule.add(schedule);
                }
            }
        }
        return listRefreshSchedule;
    }
    private List<String> parsGet(String stingUUID){
        List<String> listUUID = Arrays.asList(stingUUID.split(","));
        return listUUID;
    }
}
