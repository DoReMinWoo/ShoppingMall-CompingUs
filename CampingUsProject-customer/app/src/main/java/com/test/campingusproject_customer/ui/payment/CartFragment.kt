package com.test.campingusproject_customer.ui.payment

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.test.campingusproject_customer.databinding.FragmentCartBinding
import com.test.campingusproject_customer.databinding.RowCartBinding
import com.test.campingusproject_customer.dataclassmodel.CartModel
import com.test.campingusproject_customer.repository.CartRepository
import com.test.campingusproject_customer.ui.main.MainActivity
import com.test.campingusproject_customer.viewmodel.CartViewModel
import kotlinx.coroutines.runBlocking

class CartFragment : Fragment() {

    lateinit var mainActivity: MainActivity
    lateinit var fragmentCartBinding: FragmentCartBinding

    lateinit var cartViewModel: CartViewModel

    var countList = arrayOf(
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
        "15", "20", "25", "30", "35", "40", "45", "50", "100"
    )

    var checkedItemList = mutableListOf<CartModel>()

    val newBundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        mainActivity = activity as MainActivity
        fragmentCartBinding = FragmentCartBinding.inflate(layoutInflater)

        val sharedPreferences = mainActivity.getSharedPreferences("customer_user_info", Context.MODE_PRIVATE)

        cartViewModel = ViewModelProvider(mainActivity).get(CartViewModel::class.java)

        cartViewModel.run {
            cartProductList.observe(mainActivity) {
                fragmentCartBinding.recyclerViewCart.adapter?.notifyDataSetChanged()
            }
        }

        //하단 nav bar 안보이게
        mainActivity.activityMainBinding.bottomNavigationViewMain.visibility = View.GONE

        fragmentCartBinding.run {
            // 툴바
            toolbarCart.run {
                //백버튼 설정
                setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
                setNavigationOnClickListener {
                    mainActivity.removeFragment(MainActivity.CART_FRAGMENT)
                }
            }

            // 리사이클러 뷰
            recyclerViewCart.run {
//                cartViewModel.getCartData(sharedPreferences.getString("customerUserId", null).toString())


                adapter = CartAdapter()
                layoutManager = LinearLayoutManager(mainActivity)
            }

            // 구매하기 버튼
            buttonCartBuy.run {
                setOnClickListener {
                    newBundle.putParcelableArrayList("checkedItemList", ArrayList(checkedItemList))
                    Log.d("check", "${checkedItemList}")
                    mainActivity.replaceFragment(MainActivity.PAYMENT_FRAGMENT, true, true, newBundle)
                }
            }
        }

            cartViewModel.getCartData(sharedPreferences.getString("customerUserId", null).toString())


        return fragmentCartBinding.root
    }

    // 카트 어댑터
    inner class CartAdapter : RecyclerView.Adapter<CartAdapter.CartViewHolder>(){

        inner class CartViewHolder(rowCartBinding: RowCartBinding) : RecyclerView.ViewHolder(rowCartBinding.root) {
            val checkBoxRowCart : CheckBox
            val imageViewRowCart : ImageView
            val textViewRowCartTitle : TextView
            val buttonRowCartDelete : Button
            val spinnerRowCart : Spinner
            val textViewRowCartCost : TextView
            val progressBarRow : ProgressBar

            init {
                textViewRowCartTitle = rowCartBinding.textViewRowCartTitle
                imageViewRowCart = rowCartBinding.imageViewRowCart
                buttonRowCartDelete = rowCartBinding.buttonRowCartDelete
                textViewRowCartCost = rowCartBinding.textViewRowCartCost
                spinnerRowCart = rowCartBinding.spinnerRowCart
                checkBoxRowCart = rowCartBinding.checkBoxRowCart
                progressBarRow = rowCartBinding.progressBarRow

                spinnerRowCart.run {
                    val a1 = ArrayAdapter(mainActivity, android.R.layout.simple_spinner_item, countList)
                    a1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                    adapter = a1
                    // 기본값 0으로 설정
                    setSelection(0)

                    // 크기 조절을 원하는 크기로 설정
                    val layoutParams = layoutParams
                    layoutParams.width = 100.dpToPx()
                    this.layoutParams = layoutParams
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
            val rowCartBinding = RowCartBinding.inflate(layoutInflater)

            rowCartBinding.root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            return CartViewHolder(rowCartBinding)
        }

        override fun getItemCount(): Int {
            Log.d("size", "${cartViewModel.cartProductList.value?.size!!}")
            return cartViewModel.cartProductList.value?.size!!

//            return 10
        }

        override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
            holder.textViewRowCartTitle.text = cartViewModel.cartProductList.value?.get(position)?.productName.toString()
            holder.textViewRowCartCost.text = cartViewModel.cartProductList.value?.get(position)?.productPrice.toString()

            //상품에 등록된 이미지 경로로 첫 번째 이미지만 불러와 표시
            CartRepository.getProductFirstImage(cartViewModel.cartProductList.value?.get(position)?.productImage!!){ uri->
                //글라이드 라이브러리로 이미지 표시
                //이미지 로딩 완료되거나 실패하기 전까지 프로그래스바 활성화
                Glide.with(mainActivity).load(uri.result)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            holder.progressBarRow.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            holder.progressBarRow.visibility = View.GONE
                            return false
                        }

                    })
                    .override(200, 200)
                    .into(holder.imageViewRowCart)
            }

            holder.spinnerRowCart.run {
                onItemSelectedListener = object : OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                        val cartUserId: String
                        val cartProductId: Long
                        val cartProductCount: Long

                        cartUserId = cartViewModel.cartDataList.value?.get(position)?.cartUserId.toString()
                        cartProductId = cartViewModel.cartDataList.value?.get(position)?.cartProductId!!.toLong()
                        cartProductCount = cartViewModel.cartDataList.value?.get(position)?.cartProductCount!!.toLong()

                        val cartModel = CartModel(cartUserId, cartProductId, cartProductCount)

                        // 상품 개수 설정
                        CartRepository.setCartCount(cartModel)
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {
                        // TODO("Not yet implemented")
                    }

                }
            }
            holder.checkBoxRowCart.run {
                setOnCheckedChangeListener { compoundButton, isChecked ->

                    val cartUserId: String
                    val cartProductId: Long
                    val cartProductCount: Long

                    cartUserId = cartViewModel.cartDataList.value?.get(position)?.cartUserId.toString()
                    cartProductId = cartViewModel.cartDataList.value?.get(position)?.cartProductId!!.toLong()
                    cartProductCount = cartViewModel.cartDataList.value?.get(position)?.cartProductCount!!.toLong()

                    val cartModel = CartModel(cartUserId, cartProductId, cartProductCount)

                    if(isChecked == true) {
                        checkedItemList.add(cartModel)
                    } else {
                        checkedItemList.remove(cartModel)
                    }
                }
            }
        }
    }

    // dp 단위를 px 단위로 변환
    fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    override fun onStop() {
        super.onStop()
        mainActivity.activityMainBinding.bottomNavigationViewMain.visibility = View.VISIBLE
    }

}