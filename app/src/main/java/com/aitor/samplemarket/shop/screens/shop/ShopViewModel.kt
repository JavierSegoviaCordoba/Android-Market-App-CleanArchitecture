package com.aitor.samplemarket.shop.screens.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.None
import arrow.core.Some
import com.aitor.samplemarket.domain.usecase.AddItemToCart
import com.aitor.samplemarket.domain.model.Discount
import com.aitor.samplemarket.domain.usecase.FetchDiscounts
import com.aitor.samplemarket.domain.model.Product
import com.aitor.samplemarket.domain.usecase.FetchProducts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

typealias ShopStatusLoading = ShopViewModel.ProductStatus.Loading
typealias ShopStatusLoaded = ShopViewModel.ProductStatus.Loaded
typealias ShopStatusFailure = ShopViewModel.ProductStatus.Failure

class ShopViewModel(
    private val fetchProducts: FetchProducts,
    private val fetchDiscounts: FetchDiscounts,
    private val addItemToCart: AddItemToCart
) : ViewModel() {

    sealed class ProductStatus {
        object Loading : ProductStatus()
        data class Loaded(val products: List<Product>) : ProductStatus()
        object Failure : ProductStatus()
    }

    private val _productStatus = MutableLiveData<ProductStatus>()
    val productStatus: LiveData<ProductStatus>
        get() = _productStatus

    private val _discount = MutableLiveData<Discount>()
    val discount: LiveData<Discount>
        get() = _discount

    init {
        loadData()
    }

    fun loadData(filterCriteria: String? = null) {
        viewModelScope.launch {
            val products = withContext(Dispatchers.IO) { fetchProducts(filterCriteria) }
            val discount = withContext(Dispatchers.IO) { fetchDiscounts() }
            products.fold(ifLeft = {
                _productStatus.postValue(ProductStatus.Failure)
            }, ifRight = {
                when (discount) {
                    is Some -> {
                        it.applyDiscount(discount.t)
                        _discount.postValue(discount.t)
                    }
                    is None -> _discount.postValue(null)
                }

                _productStatus.postValue(ProductStatus.Loaded(it))
            })
        }
    }

    fun applySearchFilter(filter: String) {
        loadData(filter)
    }

    private fun List<Product>.applyDiscount(discount: Discount) {
        forEach {
            if (discount.codes.contains(it.code)) {
                it.hasDiscount = true
                if (discount is Discount.ProductDiscount) {
                    it.discountedPrice = discount.price
                }
            }
        }
    }

    fun addToCart(product: Product, amount: Int) {
        viewModelScope.launch {
            addItemToCart(product, amount)
        }
    }
}