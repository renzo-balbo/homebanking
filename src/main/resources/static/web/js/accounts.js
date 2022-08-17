const { createApp } = Vue

createApp({
  data() {
    return {
      client: {},
      accounts: [],
      loans: [],
      cards: [],
      newCardType:"",
      newCardColor:"",
    }
  },

  created() {
    this.loadClientData()
  },

  mounted() { },

  methods: {

    loadClientData() {
      axios.get("/api/clients/current")
        .then(response => {
          this.client = response.data
          this.accounts = this.client.accounts.sort((a,b)=> a.id - b.id)
          this.loans = this.client.loans.sort((a, b) => b.id - a.id)
          this.cards = this.client.cards.sort((a, b) => a.id - b.id)
          this.normalizeCardsDate(this.cards)
        })
    },

    normalizeCardsDate(cardsArray) {
      cardsArray.forEach(card => {
        card.thruDate = card.thruDate.slice(2, 7)
      })

    },

    createNewAccount() {
      axios.post("/api/clients/current/accounts", `clientEmail=${this.client.email}`, { headers: { 'content-type': 'application/x-www-form-urlencoded' } })
      .then(()=> {
        window.location.reload()
      })
      .catch(error => swal(error.response.data,{
        dangerMode:true
    }))
    },

    createNewCard(){
      axios.post("/api/clients/current/cards", `cardColor=${this.newCardColor}&cardType=${this.newCardType}`, { headers: { 'content-type': 'application/x-www-form-urlencoded' } })
      .then(()=> window.location.href="./cards.html")
      .catch(error => swal(error.response.data,{
        dangerMode:true
      }))
    },

    logout() {
      axios.post('/api/logout')
        .then(response => window.location.href = "./index.html")
    }
  },
  computed: {},

}).mount('#app')