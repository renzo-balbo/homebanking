const { createApp } = Vue

createApp({
    data() {
        return {
            logContainer: document.getElementById("app"),
            newFirstName: "",
            newLastName: "",
            newMail: "",
            newPassword: "",
            userPassword: "",
            userMail: "",
        }
    },

    created() {

    },

    mounted() { },

    methods: {
        toggleSignUpMode() {
            this.logContainer.classList.toggle("sign-up-mode")
        },
        login(email, password) {
            axios.post("/api/logout")
            .then(()=>{
                axios.post("/api/login", `email=${email}&password=${password}`, { headers: { 'content-type': 'application/x-www-form-urlencoded' } })
                .then(response => {
                    console.log(response)
                    window.location.href = "./accounts.html"
                })
                .catch(error => {
                    swal("There was an error with your email or password. Please try again.",{
                        dangerMode:true
                    });
                    console.log("Error:",error.response.status,"Code:",error.code)
                })
            })

        },
        signUp() {
            axios.post('/api/clients', `firstName=${this.newFirstName}&lastName=${this.newLastName}&email=${this.newMail}&password=${this.newPassword}`, { headers: { 'content-type': 'application/x-www-form-urlencoded' } })
            // .then(response => axios.post("/api/login", `email=${this.newMail}&password=${this.newPassword}`, { headers: { 'content-type': 'application/x-www-form-urlencoded' } }))
            .then(response => window.location.href = "./to-verify.html")
            .catch(error =>{
                console.log(error)
                console.log("Error:",error.response.status,"Code:", error.code, error.response.data)
                if(error.response.data == "This email belongs to an existing client"){
                    swal(error.response.data,".",{
                        dangerMode:true
                    })
                } else {
                    swal("Please fill all the required fields.",{
                        dangerMode:true
                    });
                }
            })
        },
    },
    computed: {},

}).mount('#app')