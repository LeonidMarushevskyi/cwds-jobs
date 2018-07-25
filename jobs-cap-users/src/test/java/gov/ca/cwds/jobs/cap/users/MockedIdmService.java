package gov.ca.cwds.jobs.cap.users;

import gov.ca.cwds.idm.dto.User;
import gov.ca.cwds.idm.dto.UsersPage;
import gov.ca.cwds.jobs.cap.users.service.IdmService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MockedIdmService implements IdmService {
  private int i = 0;

  public static int NUMBER_OF_USERS = 20;

  @Override
  public UsersPage getUserPage(String paginationToken) {
    if ((paginationToken != null) && (Integer.valueOf(paginationToken) == NUMBER_OF_USERS - 1)) {
      return new UsersPage(Collections.singletonList(createUser(++i)), null);
    }
    return new UsersPage(Collections.singletonList(createUser(i)), String.valueOf(++i));
  }

  @Override
  public List<User> getUsersByRacfIds(Set<String> racfIds) {
    List<User> users = new ArrayList<>(racfIds.size());
    for (int i = 0; i < racfIds.size(); i++) {
      users.add(createUser(i));
    }
    return users;
  }

  @Override
  public void getCapChanges() {
    //empty for now
  }

  private User createUser(int i) {
    User result = new User();
    result.setId(String.valueOf(i));
    return result;
  }
}
