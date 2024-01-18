package com.mimo.loginsignup


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp


class MainActivity : ComponentActivity() {
    private val auth: FirebaseAuth by lazy { Firebase.auth }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppContent(auth)
        }
    }

    @Composable
    fun AppContent(auth: FirebaseAuth) {
        var showSplashScreen by remember { mutableStateOf(true) }

        LaunchedEffect(showSplashScreen) {
            delay(2000)
            showSplashScreen = false
        }

        Crossfade(targetState = showSplashScreen, label = "") { isSplashScreenVisible ->
            if (isSplashScreenVisible) {
                SplashScreen {
                    showSplashScreen = false
                }
            } else {
                AuthOrMainScreen(auth)
            }
        }
    }


    @Composable
    fun SplashScreen(navigateToAuthOrMainScreen: () -> Unit) {
        // Rotate effect for the image
        var rotationState by remember { mutableFloatStateOf(0f) }

        // Navigate to AuthOrMainScreen after a delay
        LaunchedEffect(true) {
            // Simulate a delay of 2 seconds
            delay(2000)
            // Call the provided lambda to navigate to AuthOrMainScreen
            navigateToAuthOrMainScreen()
        }

        // Rotation effect animation
        LaunchedEffect(rotationState) {
            while (true) {
                delay(16) // Adjust the delay to control the rotation speed
                rotationState += 1f
            }
        }

        // Splash screen UI with transitions
        val scale by animateFloatAsState(
            targetValue = 1f,
            animationSpec = TweenSpec(durationMillis = 500), label = ""
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .scale(scale)
                    .rotate(rotationState) // Apply the rotation effect
            )
        }
    }




    @Composable
    fun AuthOrMainScreen(auth: FirebaseAuth) {
        var user by remember { mutableStateOf(auth.currentUser) }

        if (user == null) {
            AuthScreen(
                onSignedIn = { signedInUser ->
                    user = signedInUser
                }
            )
        } else {
            MainScreen(
                user = user!!,  // Pass the user information to MainScreen
                onSignOut = {
                    auth.signOut()
                    user = null
                }
            )
        }
    }


    @Composable
    fun AuthScreen(onSignedIn: (FirebaseUser) -> Unit) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }
        var isSignIn by remember { mutableStateOf(true) }
        var isPasswordVisible by remember { mutableStateOf(false) }
        // State variables for error message
        var myErrorMessage by remember { mutableStateOf<String?>(null) }


        // Load your image as ImageBitmap (replace R.drawable.your_image with your actual image resource)
        val imagePainter: Painter = painterResource(id = R.drawable.back_img)

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Background Image
            Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Create a transparent card with rounded corners
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.25f))
                    .padding(25.dp)
                    .clip(RoundedCornerShape(16.dp)),
                elevation = CardDefaults.cardElevation()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // First Name TextField
                    if (!isSignIn) {
                        Spacer(modifier = Modifier.height(8.dp))

                        TextField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            label = {
                                Text("First Name")
                            },
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Last Name TextField
                        TextField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            label = {
                                Text("Last Name")
                            },
                        )
                    }

                    // Email TextField
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = {
                            Text("Email")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email
                        ),
                        visualTransformation = if (isSignIn) VisualTransformation.None else VisualTransformation.None
                    )

                    // Password TextField
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = {
                            Text("Password")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Password
                        ),
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible }
                            ) {
                                val icon = if (isPasswordVisible) Icons.Default.Lock else Icons.Default.Search
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Toggle Password Visibility"
                                )
                            }
                        }
                    )

                    // ... (other content)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Message
                    if (myErrorMessage != null) {
                        Text(
                            text = myErrorMessage!!,
                            color = Color.Red,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign In/Sign Up Buttons
                    Button(
                        onClick = {
                            if (isSignIn) {
                                signIn(auth, email, password,
                                    onSignedIn = { signedInUser ->
                                        onSignedIn(signedInUser)
                                    },
                                    onSignInError = { errorMessage ->
                                        // Show toast message on sign-in error
                                        myErrorMessage = errorMessage
                                    }
                                )
                            } else {
                                signUp(auth, email, password, firstName, lastName) { signedInUser ->
                                    onSignedIn(signedInUser)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(8.dp),
                    ) {
                        Text(
                            text = if (isSignIn) "Sign In" else "Sign Up",
                            fontSize = 18.sp,
                        )
                    }


                    // Clickable Text
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(8.dp),
                    ) {
                        ClickableText(
                            text = AnnotatedString(buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color.Blue)) {
                                    append(if (isSignIn) "Don't have an account? Sign Up" else "Already have an account? Sign In")
                                }
                            }.toString()),
                            onClick = {
                                myErrorMessage = null
                                email = ""
                                password = ""
                                isSignIn = !isSignIn
                            },
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }
                }

            }
        }
    }
    // Function to handle sign-in errors
    private fun onSignInError(errorMessage: String) {
        // Handle the sign-in error as needed
        // For now, we'll print the error message
        println("Sign-in error: $errorMessage")
    }




    @Composable
    fun MainScreen(user: FirebaseUser, onSignOut: () -> Unit) {
        val userProfile = remember { mutableStateOf<User?>(null) }

        // Fetch user profile from Firestore
        LaunchedEffect(user.uid) {
            val firestore = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(user.uid)

            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val firstName = document.getString("firstName")
                        val lastName = document.getString("lastName")

                        userProfile.value = User(firstName, lastName, user.email ?: "")
                    } else {
                        // Handle the case where the document doesn't exist
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure

                }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            userProfile.value?.let {
                Text("Welcome, ${it.firstName} ${it.lastName}!")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSignOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Sign Out")
            }
        }
    }



    private fun signIn(
        auth: FirebaseAuth,
        email: String,
        password: String,
        onSignedIn: (FirebaseUser) -> Unit,
        onSignInError: (String) -> Unit // Callback for sign-in error
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    onSignedIn(user!!)
                } else {
                    // Handle sign-in failure
                    onSignInError("Invalid email or password")
                }
            }
    }


    private fun signUp(
        auth: FirebaseAuth,
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        onSignedIn: (FirebaseUser) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // Create a user profile in Firestore
                    val userProfile = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email
                    )

                    val firestore = FirebaseFirestore.getInstance()
                    firestore.collection("users")
                        .document(user!!.uid)
                        .set(userProfile)
                        .addOnSuccessListener {
                            onSignedIn(user)
                        }
                        .addOnFailureListener {
                            //handle exception

                        }
                } else {
                    // Handle sign-up failure

                }
            }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewAuthOrMainScreen() {
        AuthOrMainScreen(Firebase.auth)
    }

}

data class User(
    val firstName: String?,
    val lastName: String?,
    val email: String
)

