package com.ing.hubs.model.entity.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ing.hubs.exception.user.InvalidGenderException;

import java.io.IOException;


public enum Gender {
    MALE,
    FEMALE
}

