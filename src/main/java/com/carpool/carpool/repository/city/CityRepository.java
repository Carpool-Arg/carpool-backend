package com.carpool.carpool.repository.city;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carpool.carpool.model.province.city.City;

public interface CityRepository extends JpaRepository<City, Long> {

    @Query("SELECT c FROM City c WHERE LOWER(c.name) LIKE :pattern")
    List<City> findCitiesByPattern(@Param("pattern") String pattern);

    Optional<City> findByName(String name);

    @Query(value = """
        SELECT *
        FROM city c
        WHERE (
            6371 * acos(
                cos(radians(:lat)) *
                cos(radians(c.latitude)) *
                cos(radians(c.longitude) - radians(:lng)) +
                sin(radians(:lat)) *
                sin(radians(c.latitude))
            )
        ) <= 20
        ORDER BY (
            6371 * acos(
                cos(radians(:lat)) *
                cos(radians(c.latitude)) *
                cos(radians(c.longitude) - radians(:lng)) +
                sin(radians(:lat)) *
                sin(radians(c.latitude))
            )
        )
        LIMIT 1
    """, nativeQuery = true)
    Optional<City> findCityByCoordinates(@Param("lat") double latitude, @Param("lng") double longitude);
}   
