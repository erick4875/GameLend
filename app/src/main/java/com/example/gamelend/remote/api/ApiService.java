package com.example.gamelend.remote.api;

import com.example.gamelend.dto.GameDTO;
import com.example.gamelend.dto.GameResponseDTO;
import com.example.gamelend.dto.GameSummaryDTO;
import com.example.gamelend.dto.LoginRequestDTO;
import com.example.gamelend.dto.RegisterRequestDTO;
import com.example.gamelend.dto.TokenResponseDTO;
import com.example.gamelend.dto.UserDTO;
import com.example.gamelend.dto.UserResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ===== Auth Endpoints =====
    @POST("api/auth/register")
    Call<TokenResponseDTO> register(@Body RegisterRequestDTO request);

    @POST("api/auth/login")
    Call<TokenResponseDTO> login(@Body LoginRequestDTO request);

    @POST("api/auth/refresh")
    Call<TokenResponseDTO> refreshToken(@Header("Authorization") String refreshToken);


    // ===== User Endpoints =====
    @GET("api/users")
    Call<List<UserResponseDTO>> getAllUsers(@Header("Authorization") String token);

    @GET("api/users/{id}")
    Call<UserResponseDTO> getUserById(@Path("id") Long id, @Header("Authorization") String token);

    @POST("api/users")
    Call<UserResponseDTO> createUser(@Body UserDTO userDTO, @Header("Authorization") String token);

    @PUT("api/users/{id}")
    Call<UserResponseDTO> updateUser(
            @Path("id") Long id,
            @Body UserDTO userDTO,
            @Header("Authorization") String token
    );

    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Path("id") Long id, @Header("Authorization") String token);

    // ===== Búsquedas Especializadas =====
    @GET("api/users/email/{email}")
    Call<UserResponseDTO> getUserByEmail(
            @Path("email") String email,
            @Header("Authorization") String token
    );

    @GET("api/users/profile")
    Call<UserResponseDTO> getUserProfile(
            @Query("email") String email,
            @Header("Authorization") String token
    );

    @GET("api/users/{id}/complete")
    Call<UserResponseDTO> getCompleteUser(
            @Path("id") Long id,
            @Header("Authorization") String token
    );

    // ===== Game Endpoints =====
    @GET("api/games")
    Call<GameResponseDTO> createGame(@Body GameDTO gameDTO);

    @GET("api/games")
    Call<List<GameSummaryDTO>> getAllGames();

    @GET("api/games/{id}")
    Call<GameResponseDTO> getGameById(@Path("id") Long id);

    @PUT("api/games/{id}")
    Call<GameResponseDTO> updateGame(@Path("id") Long id, @Body GameDTO gameDTO);

    @DELETE("api/games/{id}")
    Call<Void> deleteGame(@Path("id") Long id);

}