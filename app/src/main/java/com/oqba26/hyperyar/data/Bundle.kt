package com.oqba26.hyperyar.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable

import androidx.room.Index

@Serializable
@Entity(tableName = "bundles")
data class Bundle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String = ""
)

@Entity(
    primaryKeys = ["bundleId", "productId"],
    indices = [Index(value = ["productId"])]
)
data class BundleProductCrossRef(
    val bundleId: Int,
    val productId: Int,
    val quantity: Double
)

data class BundleWithProducts(
    @Embedded val bundle: Bundle,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            BundleProductCrossRef::class,
            parentColumn = "bundleId",
            entityColumn = "productId"
        )
    )
    val products: List<Product>
)
