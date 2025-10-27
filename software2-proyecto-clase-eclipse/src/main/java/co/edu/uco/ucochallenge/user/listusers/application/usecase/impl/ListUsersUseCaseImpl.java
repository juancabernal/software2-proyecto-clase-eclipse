package co.edu.uco.ucochallenge.user.listusers.application.usecase.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.secondary.ports.repository.UserRepository;
import co.edu.uco.ucochallenge.user.listusers.application.interactor.mapper.ListUsersMapper;
import co.edu.uco.ucochallenge.user.listusers.application.usecase.ListUsersUseCase;
import co.edu.uco.ucochallenge.user.listusers.application.usecase.domain.ListUsersPageDomain;
import co.edu.uco.ucochallenge.user.listusers.application.usecase.domain.ListUsersQueryDomain;

@Service
@Transactional(readOnly = true)
public abstract class ListUsersUseCaseImpl implements ListUsersUseCase {

	private final UserRepository userRepository;
	private final ListUsersMapper mapper;

	public ListUsersUseCaseImpl(final UserRepository userRepository, final ListUsersMapper mapper) {
		this.userRepository = userRepository;
		this.mapper = mapper;
	}

	@Override
    public ListUsersPageDomain execute(final ListUsersQueryDomain domain) {
        final var pageable = PageRequest.of(domain.getPage(), domain.getSize());
        final var page = userRepository.findAll(pageable);
        return mapper.toPageDomain(page);
    }


}
