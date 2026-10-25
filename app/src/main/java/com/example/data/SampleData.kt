package com.example.data

import com.example.data.model.ProductItem

object SampleData {
    val defaultProducts = listOf(
        ProductItem(
            barcode = "3017620422003",
            name = "Nutella Pâte à Tartiner 400g",
            price = 3.85,
            category = "Alimentation",
            description = "Pâte à tartiner noisettes et cacao"
        ),
        ProductItem(
            barcode = "3228857000166",
            name = "Eau Minérale Naturelle 1.5L",
            price = 0.95,
            category = "Boissons",
            description = "Bouteille d'eau minérale"
        ),
        ProductItem(
            barcode = "8000500310427",
            name = "Pâtes Spaghetti N°5 500g",
            price = 1.49,
            category = "Alimentation",
            description = "Pâtes de blé dur de qualité supérieure"
        ),
        ProductItem(
            barcode = "7622210449283",
            name = "Biscuits Petit Beurre Lu 200g",
            price = 1.75,
            category = "Snacks & Épicerie",
            description = "Biscuits sablés traditionnels"
        ),
        ProductItem(
            barcode = "3046920029759",
            name = "Chocolat Noir 70% Cacao 100g",
            price = 2.30,
            category = "Confiserie",
            description = "Tablette de chocolat noir intense"
        ),
        ProductItem(
            barcode = "3124480181116",
            name = "Lait Demi-Écrémé 1L",
            price = 1.15,
            category = "Produits Laitiers",
            description = "Brique de lait français UHT"
        ),
        ProductItem(
            barcode = "3560070529841",
            name = "Café Moulu Pur Arabica 250g",
            price = 4.20,
            category = "Boissons chaudes",
            description = "Café torréfié arôme riche"
        ),
        ProductItem(
            barcode = "5449000000996",
            name = "Boisson Gazeuse Cola 33cl",
            price = 1.25,
            category = "Boissons",
            description = "Canette rafraîchissante"
        )
    )
}
