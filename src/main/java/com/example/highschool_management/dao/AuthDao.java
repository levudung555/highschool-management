package com.example.highschool_management.dao;

import com.example.highschool_management.model.UserAccount;
import java.util.*;

public interface AuthDao {
  Optional<UserAccount> timTheoTen(String username);

  Optional<UserAccount> timTheoMa(long id);

  List<String> vaiTro(long id);

  void thuHoiToken(long id);
}
