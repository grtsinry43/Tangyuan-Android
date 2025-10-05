package com.qingshuige.tangyuan.di

import com.qingshuige.tangyuan.api.ApiInterface
import com.qingshuige.tangyuan.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideUserRepository(apiInterface: ApiInterface): UserRepository {
        return UserRepository(apiInterface)
    }
    
    @Provides
    @Singleton
    fun providePostRepository(apiInterface: ApiInterface): PostRepository {
        return PostRepository(apiInterface)
    }
    
    @Provides
    @Singleton
    fun provideCommentRepository(apiInterface: ApiInterface): CommentRepository {
        return CommentRepository(apiInterface)
    }
    
    @Provides
    @Singleton
    fun provideCategoryRepository(apiInterface: ApiInterface): CategoryRepository {
        return CategoryRepository(apiInterface)
    }
    
    @Provides
    @Singleton
    fun provideNotificationRepository(apiInterface: ApiInterface): NotificationRepository {
        return NotificationRepository(apiInterface)
    }
    
    @Provides
    @Singleton
    fun provideMediaRepository(apiInterface: ApiInterface): MediaRepository {
        return MediaRepository(apiInterface)
    }
    
    @Provides
    @Singleton
    fun providePostDetailRepository(
        apiInterface: ApiInterface,
        postRepository: PostRepository,
        userRepository: UserRepository
    ): PostDetailRepository {
        return PostDetailRepository(apiInterface, postRepository, userRepository)
    }
}