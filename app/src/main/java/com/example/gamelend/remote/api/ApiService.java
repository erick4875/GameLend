package com.example.gamelend.remote.api;

import com.example.gamelend.dto.DocumentResponseDTO;
import com.example.gamelend.dto.GameDTO;
import com.example.gamelend.dto.GameResponseDTO;
import com.example.gamelend.dto.GameSummaryDTO;
import com.example.gamelend.dto.LoanRequestDTO;
import com.example.gamelend.dto.LoanResponseDTO;
import com.example.gamelend.dto.LoanReturnDTO;
import com.example.gamelend.dto.LoginRequestDTO;
import com.example.gamelend.dto.RegisterRequestDTO;
import com.example.gamelend.dto.TokenResponseDTO;
import com.example.gamelend.dto.UserDTO;
import com.example.gamelend.dto.UserResponseDTO;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
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
    @Multipart
    @POST("api/users/{userId}/photo")
    Call<UserResponseDTO> uploadProfileImage(
            @Path("userId") Long userId,
            @Part MultipartBody.Part file
    );

    @GET("api/users")
    Call<List<UserResponseDTO>> getAllUsers();

    @GET("api/users/{id}")
    Call<UserResponseDTO> getUserById(@Path("id") Long id);

    @POST("api/users")
    Call<UserResponseDTO> createUser(@Body UserDTO userDTO);

    @PUT("api/users/{id}")
    Call<UserResponseDTO> updateUser(
            @Path("id") Long id,
            @Body UserDTO userDTO
    );

    @DELETE("api/users/{id}")
    Call<Void> deleteUser(@Path("id") Long id);

    @GET("api/users/email/{email}")
    Call<UserResponseDTO> getUserByEmail(@Path("email") String email);

    @GET("api/users/profile")
    Call<UserResponseDTO> getUserProfile(@Query("email") String email);

    @GET("api/users/{id}/complete")
    Call<UserResponseDTO> getCompleteUser(@Path("id") Long id);


    // ===== Game Endpoints =====
    @POST("api/games")
    Call<GameResponseDTO> createGame(@Body GameDTO gameDTO);

    @GET("api/games")
    Call<List<GameSummaryDTO>> getAllGames();

    @GET("api/games/user/{userId}")
    Call<List<GameResponseDTO>> getGamesByUserId(@Path("userId") Long userId);

    @GET("api/games/{id}")
    Call<GameResponseDTO> getGameDetailsById(@Path("id") Long gameId);

    @PUT("api/games/{id}")
    Call<GameResponseDTO> updateGame(@Path("id") Long id, @Body GameDTO gameDTO);

    @DELETE("api/games/{id}")
    Call<Void> deleteGame(@Path("id") Long id);

    @POST("api/loans/request")
    Call<LoanResponseDTO> requestLoan(@Body LoanRequestDTO loanRequest);

    @PUT("api/loans/{loanId}/return")
    Call<LoanResponseDTO> recordLoanReturn(@Path("loanId") Long loanId, @Body LoanReturnDTO loanReturnDTO);
}
