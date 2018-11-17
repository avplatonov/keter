package ru.avplatonov.keter.backend.controllers.monitoring.schedule;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.avplatonov.keter.backend.initialize.monitoring.schedule.Schedule;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static ru.avplatonov.keter.backend.db.ScheduleDB.listSchedule;

@RestController
@EnableAutoConfiguration
public class RefreshScheduleState {
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
