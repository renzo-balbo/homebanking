const { createApp } = Vue

createApp({
    data() {
    return {
        clientList: [],
        newClientFirtsName:"",
        newClientLastName:"",
        newClientEmail:"",
        newClientData:[],
    }
    },
    created(){
        this.loadData()




    },
    mounted(){

    },

    methods:{

        loadData(){
            axios.get('/api/clients')
            .then((respuesta)=>{
                this.clientList = respuesta.data
            })
        },

        addClient(){
            axios.post('/rest/clients',{
            firstName: this.newClientFirtsName,
            lastName: this.newClientLastName,
            email: this.newClientEmail,
            })
            .then((response)=>{
                this.clientList.push(response)
            })
            .then(()=>this.postClient())
        },

        postClient(){
        axios.post('/rest/clients')
            .then(()=>this.loadData())
        },

        deleteClient(clientSelected){
            axios.delete("rest/clients/"+clientSelected.id)
            .then(response =>{
                console.log(response)
                this.loadData()
            })
            .catch(error =>{
                console.log('f')
            })
        },
    
        editClient(clientSelected){
            let newEmail
            newEmail = prompt("Enter your new mail")
            clientSelected={
                email: newEmail
            } 
            axios.patch("rest/clients/"+clientSelected.id, clientSelected)
            .then(()=>this.loadData())
        }




    },

    computed:{

    },

  }).mount('#app')

