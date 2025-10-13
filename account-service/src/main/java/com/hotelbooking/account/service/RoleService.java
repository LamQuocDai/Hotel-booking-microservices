package com.hotelbooking.account.service;

import com.hotelbooking.account.dto.RoleDTO;
import com.hotelbooking.account.entity.Role;
import com.hotelbooking.account.repository.RoleRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

@Service
public class RoleService{
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public RoleDTO createRole(RoleDTO roleDTO) throws BadRequestException {
        if (roleRepository.findByNameIgnoreCase(roleDTO.getName()).isPresent()) {
            throw new BadRequestException("Role with name " + roleDTO.getName() + " already exists.");
        }

        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());

        return new RoleDTO(roleRepository.save(role));
    }
}
