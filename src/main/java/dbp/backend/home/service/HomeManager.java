package dbp.backend.home.service;

import dbp.backend.home.dto.response.HomeMeetupCardResponse;
import dbp.backend.meetup.dao.MeetupDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeManager {
    private final MeetupDao meetupDao;

    public HomeManager(MeetupDao meetupDao) {
        this.meetupDao = meetupDao;
    }

    public List<HomeMeetupCardResponse>
    getRecentMeetups() {
        return meetupDao.findRecentMeetups();
    }
}