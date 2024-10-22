package com.layth.Library.Management.System.repositories;

import com.layth.Library.Management.System.entities.Patron;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatronsRepository extends JpaRepository<Patron,Integer> {
}
