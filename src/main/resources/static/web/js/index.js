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
        login() {
            axios.post("/api/login", `email=${this.userMail}&password=${this.userPassword}`, { headers: { 'content-type': 'application/x-www-form-urlencoded' } })
                .then(response => window.location.href = "./accounts.html")
        },
        signUp() {
            axios.post('/api/clients', `firstName=${this.newFirstName}&lastName=${this.newLastName}&email=${this.newMail}&password=${this.newPassword}`, { headers: { 'content-type': 'application/x-www-form-urlencoded' } })
            .then(response => axios.post("/api/login", `email=${this.newMail}&password=${this.newPassword}`, { headers: { 'content-type': 'application/x-www-form-urlencoded' } }))
            .then(response => window.location.href = "./accounts.html")
        },
    },
    computed: {},

}).mount('#app')