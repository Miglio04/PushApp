// Questo file serve per leggere calorie, proteine, grassi, carboidrati

package com.example.pushapp.models.api.food;
import com.google.gson.annotations.SerializedName;

public class Nutrients {
    @SerializedName("ENERC_KCAL") public double calories;
    @SerializedName("PROCNT")     public double protein;
    @SerializedName("FAT")        public double fat;
    @SerializedName("CHOCDF")     public double carbs;
}