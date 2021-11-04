package ca.gbc.comp3095.cookbook.services;

import ca.gbc.comp3095.cookbook.model.User;

public interface UserService extends CrudService<User, Long>{

    boolean checkCredentials(User user);
}
