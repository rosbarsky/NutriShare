package net.nutrishare.app.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import net.nutrishare.app.adapters.ProductAdapter
import net.nutrishare.app.data.ProductRepository
import net.nutrishare.app.database.AppDatabase
import net.nutrishare.app.databinding.FragmentOpenFoodFactsBinding
import net.nutrishare.app.factory.ProductViewModelFactory
import net.nutrishare.app.model.Product
import net.nutrishare.app.retrofit.RetrofitClient
import net.nutrishare.app.utils.WrapContentLinearLayoutManager
import net.nutrishare.app.viewmodel.ProductViewModel


class OpenFoodFactsFragment : Fragment() {

    private lateinit var binding:FragmentOpenFoodFactsBinding
    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: ProductAdapter
    private var productsList = mutableListOf<Product>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val productDao = AppDatabase.getDatabase(requireContext()).productDao()
        val repository = ProductRepository(productDao, RetrofitClient.api)
        val viewModelFactory = ProductViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(),viewModelFactory)[ProductViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOpenFoodFactsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setUpRecyclerView()
        setUpListeners()
    }

    private fun observeViewModel(){

        viewModel.products.observe(viewLifecycleOwner) { products ->
            binding.searchButton.text = "Search"
            binding.searchButton.isEnabled = true
            binding.searchEditText.isEnabled = true
            if (products.isNotEmpty()){
                productsList.clear()
                productsList.addAll(products)
                adapter.notifyItemChanged(0,productsList.size)
                binding.productsRecyclerview.visibility = View.VISIBLE
                binding.emptyListView.visibility = View.GONE
            }
            else{
                binding.productsRecyclerview.visibility = View.GONE
                binding.emptyListView.visibility = View.VISIBLE
            }
        }

        viewModel.categoryProducts.observe(viewLifecycleOwner) { products ->
            binding.searchCategoryButton.text = "Filter"
            binding.searchCategoryButton.isEnabled = true
            binding.searchCategoryEditText.isEnabled = true
            if (products.isNotEmpty()){
                productsList.clear()
                productsList.addAll(products)
                adapter.notifyItemChanged(0,productsList.size)
                binding.productsRecyclerview.visibility = View.VISIBLE
                binding.emptyListView.visibility = View.GONE
            }
            else{
                binding.productsRecyclerview.visibility = View.GONE
                binding.emptyListView.visibility = View.VISIBLE
            }
        }

        viewModel.searchProducts("")

    }

    private fun setUpRecyclerView(){
        binding.productsRecyclerview.layoutManager = WrapContentLinearLayoutManager(requireActivity(),
            RecyclerView.VERTICAL,false)
        binding.productsRecyclerview.hasFixedSize()
        adapter = ProductAdapter(productsList)
        binding.productsRecyclerview.adapter = adapter
    }

    private fun setUpListeners(){

        binding.searchButton.setOnClickListener {
            binding.searchButton.text = "Please wait..."
            binding.searchButton.isEnabled = false
            binding.searchEditText.isEnabled = false
            val query = binding.searchEditText.text.toString().trim()
            viewModel.searchProducts(query)
        }

       binding.searchCategoryButton.setOnClickListener {
           binding.searchCategoryButton.text = "Please wait..."
           binding.searchCategoryButton.isEnabled = false
           binding.searchCategoryEditText.isEnabled = false
           val category = binding.searchCategoryEditText.text.toString().trim()
           viewModel.searchProductsByCategory(category)
       }
    }


}