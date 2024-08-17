package com.codegym.telegram;

public class UserInfo {
    public String name; //Nombre
    public String sex; //Sexo
    public String age; //Edad
    public String city; //Ciudad
    public String occupation; //Profesión
    public String hobby; //Hobby
    public String handsome; //Belleza, atractivo
    public String wealth; //Ingresos, riqueza
    public String annoys; //Lo que me molesta en las personas
    public String goals; //Objetivos de la relación

    private String fieldToString(String str, String description) {
        if (str != null && !str.isEmpty())
            return description + ": " + str + "\n";
        else
            return "";
    }

    @Override
    public String toString() {
        String result = "";

        result += fieldToString(name, "Nombre");
        result += fieldToString(sex, "Sexo");
        result += fieldToString(age, "Edad");
        result += fieldToString(city, "Ciudad");
        result += fieldToString(occupation, "Profesión");
        result += fieldToString(hobby, "Hobby");
        result += fieldToString(handsome, "Belleza, atractivo en puntos (máximo 10 puntos)");
        result += fieldToString(wealth, "Ingresos, riqueza");
        result += fieldToString(annoys, "Lo que molesta en las personas");
        result += fieldToString(goals, "Objetivos de la relación");

        return result;
    }
}
