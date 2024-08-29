package com.Oleg.smartAgentBot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Apartments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Double price;
    private String phoneNumber;
    private String ownerName;
    private String description;
    private String district;
    private String street;
    private String apartmentNumber;
    private long userId;


    @Override
    public String toString() {
        return String.format(
                "        \uD83C\uDFE0 Апартаменты №%s\n\n" +
                        "\uD83D\uDDC2 Вид: %s \n" +
                        "\uD83D\uDDFA Район: %s\n" +
                        "\uD83C\uDFD9 Улица: %s\n" +
                        "\uD83E\uDD11 Цена: %.2f\n" +
                        "\uD83D\uDCDE Телефон: %s\n" +
                        "\uD83D\uDC68\uD83C\uDFFC\u200D\uD83E\uDDB0 Владелец: %s\n" +
                        "\uD83D\uDCD4 Описание: %s\n",
                apartmentNumber, name, district, street, price, phoneNumber, ownerName, description
        );
    }
}