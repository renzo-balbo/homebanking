const { createApp } = Vue

createApp({
  data() {
    return {
        queryString:"",
        params: "",
        id:"",
        account:{},
        transactions:[],
        client:{},
        fromDate:"",
        toDate:""
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
            this.account.balance = this.moneyFormatter(this.account.balance)
            this.transactions = this.account.transactions.sort((a,b) => b.id - a.id)
            this.transactions.forEach(transaction=>{
              transaction.amount = this.moneyFormatter(transaction.amount)
              transaction.oldBalance= this.moneyFormatter(transaction.oldBalance)
              transaction.postTransaction= this.moneyFormatter(transaction.postTransaction)
            })
            this.normalizeDate(this.transactions)
            this.loadClientData()
        })
    },

    loadClientData(){
      axios.get("/api/clients/current")
      .then(response => {
        this.client=response.data
        this.client.accounts = this.client.accounts.filter(account=> account.active==true)
      })
    },

    normalizeDate(transactionsArray){
      transactionsArray.forEach(transaction => {
        transaction.date = transaction.date.slice(0, 10) 
      });
    },

    generateTransactionPdf(){
      this.fromDate = new Date(this.fromDate).toISOString()
      this.toDate = new Date(this.toDate).toISOString()
      axios.post('/api/transactions/filtered', {fromDate:`${this.fromDate}`,toDate:`${this.toDate}`,accountNumber:`${this.account.number}`})
      .then(response=>{
        console.log(response)
      })
      .catch(error=>console.log(error.response.data))
    },

    logout() {
      axios.post('/api/logout')
        .then(response => window.location.href = "./index.html")
    },

    moneyFormatter(numberToFormat){
      let formatter = new Intl.NumberFormat('en-US',{
        style: 'currency',
        currency:'USD',
      })
      return formatter.format(numberToFormat)
    },

  },

  computed: {},

}).mount('#app')