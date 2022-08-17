const { createApp } = Vue

createApp({
  data() {
    return {
        queryString:"",
        params: "",
        id:"",
        account:{},
        transactions:[],
        client:{}
    }
  },

  created() {
    this.queryString = location.search
    this.params = new URLSearchParams(this.queryString)
    this.id = this.params.get("id")
    this.loadData()
    

  },

  mounted() { },

  methods: {

    loadData(){
        axios.get("/api/accounts/"+this.id)
        .then(response =>{
            this.account = response.data
            this.transactions = this.account.transactions.sort((a,b) => b.id - a.id)
            this.normalizeDate(this.transactions)
            this.loadClientData()
        })
    },

    loadClientData(){
      axios.get("/api/clients/current")
      .then(response => this.client=response.data)
    },

    normalizeDate(transactionsArray){
      transactionsArray.forEach(transaction => {
        transaction.date = transaction.date.slice(0, 10) 
      });
    },

    logout() {
      axios.post('/api/logout')
        .then(response => window.location.href = "./index.html")
    },

  },

  computed: {},

}).mount('#app')