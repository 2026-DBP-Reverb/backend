package dbp.backend.meetup.service;

import dbp.backend.meetup.dao.MeetupDao;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MeetupStatusScheduler {

    private final MeetupDao meetupDao;

    public MeetupStatusScheduler(MeetupDao meetupDao){
        this.meetupDao = meetupDao;
    }

    @Scheduled(fixedDelay = 60_000)
    public void completeStartedMeetups(){
        meetupDao.completeStartedMeetups();
    }
}
