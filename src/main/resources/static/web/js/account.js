const { createApp } = Vue

createApp({
  data() {
    return {
        queryString:"",
        params: "",
        id:"",
        account:{},
        transactions:[]
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
        })
    },

    normalizeDate(transactionsArray){
      transactionsArray.forEach(transaction => {
        transaction.date = transaction.date.slice(0, 10) 
      });
    },

  },

  computed: {},

}).mount('#app')