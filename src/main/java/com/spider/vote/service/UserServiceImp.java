package com.spider.vote.service;

import com.spider.vote.domain.entity.User;
import com.spider.vote.repository.UserRepository;
import com.spider.vote.service.interfaces.UserService;
import com.spider.vote.utils.exceptions.NotFoundException;
import com.spider.vote.web.AuthorizedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

@Service("userService")
public class UserServiceImp implements UserService, UserDetailsService {

    @Autowired
    private UserRepository repository;


    @Override
    @Transactional
    public User getUserByEmail(String email) throws NotFoundException {
        Assert.notNull(email, "email must be not null");
        User user= repository.getUserByEmail(email);
        if (user==null){
            throw new NotFoundException("User: " + email + " was not found");
        }
        if (!user.isEnabled()){
            throw new DisabledException("User: " + email + " is banned");
        }
        return user;
    }

    @Cacheable("users")
    @Override
    public List<User> getAll() {
        return repository.getAll();
    }

    @CacheEvict(value = "users", allEntries = true)
    @Override
    public void delete(int id) {
        repository.delete(id);
    }

    @CacheEvict(value = "users", allEntries = true)
    @Override
    public void update(User user) {
        Assert.notNull(user,"User must be not null ");
      repository.save(user);
    }

    @CacheEvict(value = "users", allEntries = true)
    @Override
    public void evictCache() {
        // only for evict cache
    }

    @CacheEvict(value = "users", allEntries = true)
    public User create(User user) {
        Assert.notNull(user, "user must not be null");
        return repository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.getUserByEmail(email.toLowerCase());
        if (user == null) {
            throw new UsernameNotFoundException("User " + email + " is not found");
        }
        return new AuthorizedUser(user);
    }
}
