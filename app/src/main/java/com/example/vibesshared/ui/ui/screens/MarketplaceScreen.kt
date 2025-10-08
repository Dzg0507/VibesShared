package com.example.vibesshared.ui.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.vibesshared.ui.ui.ecommerce.*
import com.example.vibesshared.ui.ui.theme.*
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val marketplaceService = remember { MarketplaceService(context) }
    val scope = rememberCoroutineScope()
    
    var currentTab by remember { mutableStateOf(MarketplaceTab.SHOP) }
    var featuredShops by remember { mutableStateOf<List<Shop>>(emptyList()) }
    var featuredProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Product>>(emptyList()) }
    
    // Load data
    LaunchedEffect(Unit) {
        featuredShops = marketplaceService.getFeaturedShops()
        featuredProducts = marketplaceService.getFeaturedProducts()
        cartItems = marketplaceService.getCart("current_user")
        orders = marketplaceService.getUserOrders("current_user")
    }
    
    val gradientColors = listOf(ElectricPurple, NeonPink, VividBlue, SunsetOrange, LimeGreen)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.linearGradient(colors = gradientColors))
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Store,
                                contentDescription = "Marketplace",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Marketplace", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Open cart */ }) {
                            Box {
                                Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = "Cart", tint = Color.White)
                                if (cartItems.isNotEmpty()) {
                                    Badge(
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text("${cartItems.size}")
                                    }
                                }
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* Create shop */ },
                    containerColor = LimeGreen
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Create Shop", tint = Color.White)
                }
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        if (query.isNotBlank()) {
                            scope.launch {
                                searchResults = marketplaceService.searchProducts(query)
                            }
                        }
                    },
                    placeholder = { Text("Search products, shops...", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color.White
                    ),
                    trailingIcon = {
                        Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                    }
                )
                
                if (searchQuery.isNotBlank()) {
                    SearchResultsSection(results = searchResults)
                } else {
                    // Tab Row
                    TabRow(
                        selectedTabIndex = currentTab.ordinal,
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ) {
                        MarketplaceTab.values().forEachIndexed { index, tab ->
                            Tab(
                                selected = currentTab == tab,
                                onClick = { currentTab = tab },
                                text = { Text(tab.title, color = Color.White) },
                                icon = { Icon(imageVector = tab.icon, contentDescription = tab.title, tint = Color.White) }
                            )
                        }
                    }
                    
                    // Content based on selected tab
                    when (currentTab) {
                        MarketplaceTab.SHOP -> ShopTab(shops = featuredShops)
                        MarketplaceTab.PRODUCTS -> ProductsTab(products = featuredProducts)
                        MarketplaceTab.CART -> CartTab(cartItems = cartItems)
                        MarketplaceTab.ORDERS -> OrdersTab(orders = orders)
                    }
                }
            }
        }
    }
}

@Composable
fun ShopTab(shops: List<Shop>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Featured Shops",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(shops) { shop ->
            ShopCard(shop = shop)
        }
    }
}

@Composable
fun ShopCard(shop: Shop) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = shop.logoUrl,
                    contentDescription = "Shop Logo",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shop.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Text(
                        text = shop.category,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = String.format("%.1f", shop.rating),
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            text = "${shop.totalSales} sales",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                if (shop.isVerified) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Verified",
                        tint = VividBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = shop.description,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                shop.tags.forEach { tag ->
                    Text(
                        text = tag,
                        fontSize = 10.sp,
                        color = ElectricPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(ElectricPurple.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductsTab(products: List<Product>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Featured Products",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(products) { product ->
            ProductCard(product = product)
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.images.firstOrNull(),
                contentDescription = "Product Image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricPurple
                    )
                    
                    if (product.isOnSale && product.originalPrice != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$${String.format("%.2f", product.originalPrice)}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.background(Color.Gray.copy(alpha = 0.2f))
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = String.format("%.1f", product.rating),
                        fontSize = 12.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "(${product.reviewCount} reviews)",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.FavoriteBorder,
                        contentDescription = "Add to Wishlist",
                        tint = Color.Gray
                    )
                }
                
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Add to Cart",
                        tint = ElectricPurple
                    )
                }
            }
        }
    }
}

@Composable
fun CartTab(cartItems: List<CartItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Shopping Cart",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        if (cartItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Empty Cart",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your cart is empty",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Add some products to get started!",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(cartItems) { cartItem ->
                CartItemCard(cartItem = cartItem)
            }
        }
    }
}

@Composable
fun CartItemCard(cartItem: CartItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https://picsum.photos/80/80?id=${cartItem.productId}",
                contentDescription = "Product Image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Product ${cartItem.productId}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = "Quantity: ${cartItem.quantity}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Text(
                    text = "$${String.format("%.2f", 25.99 * cartItem.quantity)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricPurple
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Remove",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun OrdersTab(orders: List<Order>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Your Orders",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(orders) { order ->
            OrderCard(order = order)
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id.substring(0, 8)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Text(
                    text = order.status.name,
                    fontSize = 12.sp,
                    color = when (order.status) {
                        OrderStatus.PENDING -> SunsetOrange
                        OrderStatus.CONFIRMED -> VividBlue
                        OrderStatus.SHIPPED -> ElectricPurple
                        OrderStatus.DELIVERED -> LimeGreen
                        OrderStatus.CANCELLED -> Color.Red
                        OrderStatus.RETURNED -> Color.Gray
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            when (order.status) {
                                OrderStatus.PENDING -> SunsetOrange.copy(alpha = 0.1f)
                                OrderStatus.CONFIRMED -> VividBlue.copy(alpha = 0.1f)
                                OrderStatus.SHIPPED -> ElectricPurple.copy(alpha = 0.1f)
                                OrderStatus.DELIVERED -> LimeGreen.copy(alpha = 0.1f)
                                OrderStatus.CANCELLED -> Color.Red.copy(alpha = 0.1f)
                                OrderStatus.RETURNED -> Color.Gray.copy(alpha = 0.1f)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${order.items.size} item(s)",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Text(
                text = "Total: $${String.format("%.2f", order.totalAmount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ElectricPurple
            )
            
            Text(
                text = "Tracking: ${order.trackingNumber}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SearchResultsSection(results: List<Product>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Search Results",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        if (results.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SearchOff,
                            contentDescription = "No Results",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No products found",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Try a different search term",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(results) { product ->
                ProductCard(product = product)
            }
        }
    }
}

enum class MarketplaceTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SHOP("Shops", Icons.Filled.Store),
    PRODUCTS("Products", Icons.Filled.ShoppingBag),
    CART("Cart", Icons.Filled.ShoppingCart),
    ORDERS("Orders", Icons.Filled.Receipt)
}