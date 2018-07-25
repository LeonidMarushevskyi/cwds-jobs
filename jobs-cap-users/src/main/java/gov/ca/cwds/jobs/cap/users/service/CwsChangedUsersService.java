package gov.ca.cwds.jobs.cap.users.service;

import com.google.inject.Inject;
import gov.ca.cwds.idm.dto.User;
import gov.ca.cwds.jobs.cap.users.dao.CwsUsersDao;
import gov.ca.cwds.jobs.cap.users.dto.ChangedUserDto;
import gov.ca.cwds.jobs.common.RecordChangeOperation;
import gov.ca.cwds.jobs.common.savepoint.LocalDateTimeSavePointContainer;
import gov.ca.cwds.jobs.common.savepoint.LocalDateTimeSavePointService;
import org.apache.shiro.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CwsChangedUsersService {

  @Inject
  private LocalDateTimeSavePointService savePointService;

  @Inject
  private IdmService idmService;

  @Inject
  private CwsUsersDao dao;

  public List<ChangedUserDto> getCwsChanges() {
    LocalDateTime savePointTime = savePointService.loadSavePoint(LocalDateTimeSavePointContainer.class).getTimestamp();
    Set<String> changedRacfIds = dao.getChangedRacfIds(savePointTime);
    if (CollectionUtils.isEmpty(changedRacfIds)) {
      return Collections.emptyList();
    }
    List<User> users = idmService.getUsersByRacfIds(changedRacfIds);
    return users.stream()
            .map(e -> new ChangedUserDto(e, RecordChangeOperation.U))
            .collect(Collectors.toList());
  }
}
