package at.ac.tuwien.ase.groupphase.backend.service;

import at.ac.tuwien.ase.groupphase.backend.endpoint.dto.locator.LocatedObjectDto;

import java.util.List;

public interface LocatorService {

    List<LocatedObjectDto> getLocatedObjectsFromFarmer();
}
