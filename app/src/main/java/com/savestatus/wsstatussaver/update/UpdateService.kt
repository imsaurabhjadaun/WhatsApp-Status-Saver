package com.savestatus.wsstatussaver.update

import retrofit2.http.GET
import retrofit2.http.Path

interface UpdateService {
    @GET("{user}/{repo}/releases/latest")
    suspend fun latestRelease(@Path("user") user: String, @Path("repo") repository: String): GitHubRelease
}