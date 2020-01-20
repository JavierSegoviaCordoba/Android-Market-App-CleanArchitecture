package com.aitor.samplemarket.repository

import arrow.core.Option
import arrow.core.some
import com.aitor.samplemarket.BaseTest
import com.aitor.samplemarket.DiscountMother.PRODUCT_DISCOUNT
import com.aitor.samplemarket.discount.network.FakeNetworkDiscountDataSource
import com.aitor.samplemarket.discount.network.NetworkDiscount
import com.aitor.samplemarket.discount.repository.DiscountRepositoryImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DiscountRepositoryTest : BaseTest() {

    private val fakeNetworkDiscountDataSource = mockk<FakeNetworkDiscountDataSource>()
    private lateinit var discountRepository: DiscountRepositoryImpl

    @Before
    fun setup() {
        discountRepository = DiscountRepositoryImpl(fakeNetworkDiscountDataSource)
    }

    @Test
    fun `gets a correct discount`() {
        val expectedDiscount = givenADiscount()
        val testDiscount = discountRepository.fetchDiscounts()

        assertEquals(expectedDiscount.map { it.asDomainModel() }, testDiscount)
    }

    private fun givenADiscount(): Option<NetworkDiscount> {
        val discount = PRODUCT_DISCOUNT.some()
        every { fakeNetworkDiscountDataSource.all } returns discount
        return discount
    }
}