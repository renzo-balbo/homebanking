const { createApp } = Vue

createApp({
  data() {
    return {
      client: {},
      accounts: [],
      loans: [],
      cards: []
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
          this.accounts = this.client.accounts
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

    logout() {
      axios.post('/api/logout')
        .then(response => window.location.href="./index.html")
    }
  },
  computed: {},

}).mount('#app')