package com.example.pushapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pushapp.models.User;

/**
 * Data Access Object (DAO) for accessing User data.
 * Manages local storage of user profiles.
 */
@Dao
public interface UserDao {
    /**
     * Inserts a user into the database. Replaces on conflict.
     *
     * @param user The user to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    /**
     * Updates an existing user record.
     *
     * @param user The user with updated data.
     */
    @Update
    void update(User user);

    /**
     * Deletes a user record.
     *
     * @param user The user to delete.
     */
    @Delete
    void delete(User user);

    /**
     * Retrieves a user by their unique user ID.
     *
     * @param uid The unique ID of the user.
     * @return The User object or null if not found.
     */
    @Query("SELECT * FROM user WHERE userId = :uid")
    User getByUserId(String uid);

    /**
     * Deletes all users from the database.
     */
    @Query("DELETE FROM user")
    void deleteAll();

}
