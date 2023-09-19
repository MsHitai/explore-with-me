package ru.practicum.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.UserDto;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto addUser(UserDto dto) {
        User user = UserMapper.mapToUser(dto);
        return UserMapper.mapToDto(userRepository.save(user));
    }

    @Override
    public void deleteUserById(Long userId) {
        try {
            userRepository.deleteById(userId);
        } catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException(String.format("User with the id=%d was not found in the database", userId));
        }
    }

    @Override
    public List<UserDto> findAllUsers(List<Long> ids, Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        if (ids == null) {
            return userRepository.findAll(page).stream()
                    .map(UserMapper::mapToDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAllByIdIn(ids, page).stream()
                    .map(UserMapper::mapToDto)
                    .collect(Collectors.toList());
        }
    }
}
