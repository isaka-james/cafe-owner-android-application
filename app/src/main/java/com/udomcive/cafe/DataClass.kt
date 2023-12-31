package com.udomcive.cafe

data class Category(
    val id: String,
    val image_url: String,
    val title: String
)

data class CardData(val imageUrl: String, val title: String)



data class Order(
    val orderId: String,
    val imageResource: String,
    val restaurantName: String,
    val orderDate: String,
    val orderStatus: String,
    val deliveryTime: String
)

data class User(
    val username: String,
    val email: String,
    val phone: String,
    val location: String
)


data class SingleProduct(
    val id: String,
    val image_url: String,
    val name: String,
    val price: String,
    val location: String,
    val oglocation: String,
    val comment: String,
    val time: String,
    val phone: String,
    val from: String,
    val customer: String
)

