package com.eyther.lumbridge.features.authentication.data.fixtures

internal fun sessionResponseFixture(): String = """{
                        "access_token":"test-access-token",
                        "refresh_token":"test-refresh-token",
                        "token_type":"bearer",
                        "expires_in":3600,
                        "user":{
                            "id":"11111111-1111-1111-1111-111111111111",
                            "aud":"authenticated",
                            "email":"member@example.com",
                            "created_at":"2026-11-14T00:00:00Z",
                            "app_metadata":{"provider":"google"},
                            "user_metadata":{}
                        }
                    }"""
