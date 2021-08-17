# budget_helper_backend
budget_helper_backend is backend part of application used in my project for engineering degree

link to front end part can be found here: https://github.com/mlipinski97/budget_helper

Each controller handles part of app:

-UserController handles budget list related actions
                -(/api/users/getall) retrieves list of all app users 
                -(/api/users/getbyusername) fetches single user by given username
                -(/api/users/getallbybudgetlistid) retrieves all users signed to given budget list
                -(/api/users/delete) deletes user with given username
                -(/api/users/register) allows to register new user (with normal permissions)
                -(/api/users/registeradmin) allows to register new admin user
                -(/api/users/account) fetches currently loggin in user
  
-CategoryController handles action related to expenses categories
  -(/api/category/getall) fetches all categories (even those marked as "deleted")
  -(/api/category/getbyname) retrieves category with given name
  -(/api/category/add) allows to add new category
  -(/api/category/edit) allows to edit selected category
  -(/api/category/delete) allows to delete category (categories are never deleted, deleting category ensures that no new expenses can be added with that category but old expenses still are signed to that category)
  -(/api/category/deletemany) allows to delete many categories with one query
  
-ExpenseController handles actions related to expenses
  -(/api/expenses/getall) fetches all expenses on server
  -(/api/expenses/getbyid) retrieves single expense by id
  -(/api/expenses/getallbybudgetlist) retrieves all expeenses on selected budget list
  -(/api/expenses/deletebyid) deletes single expense by id
  -(/api/expenses/deletemany) deletes many expenses with one query
  -(/api/expenses/add) allows to add new expense to selected budget list
  -(/api/expenses/complete) changes state of selected expense to "completed"
  -(/api/expenses/undocomplete) changes state of selected expense to "not completed"
  -(/api/expenses/changedonestate) changes state of selected expense from current to opposing
  -(/api/expenses/edit) allows to edit selected expense
  -(/api/expenses/getmonthstatistics) fetches statistics for selected date (year and month). Statistics compose of expenses in selected currency from selected month
  

-FriendshipController handle all actions related to friendships between app users
  -(/api/users/friendship/getallfriends) fetches all friends of given user
  -(/api/users/friendship/findFriendship) tries to find friendship between logged user and given user
  -(/api/users/friendship/add) adds new friendship between two users
  -(/api/users/friendship/delete) deletes selected friendship relation
  -(/api/users/friendship/deletemany) deletes many friendship relations
  -(/api/users/friendship/accept) sets friendship status as "accepted" (all new friendships are "pending" after being created)
  
-BudgetListController handles users related actions
  -(/api/budgetlist/getall) fetches all budget lists
  -(/api/budgetlist/getbyname) retrieves budget list with given name
  -(/api/budgetlist/getallbyuser) retrives all budget lists that selected user has access to
  -(/api/budgetlist/getearliestdateforuser) fetches budget list with most soon ending date for selected user
  -(/api/budgetlist/getbyid) fetches budget list with given id
  -(/api/budgetlist/delete) deletes selected budget list
  -(/api/budgetlist/deletemany) deletes many budget lists
  -(/api/budgetlist/add) adds new budget list
  -(/api/budgetlist/edit) allows to edit selected budget list
  -(/api/budgetlist/share) shares selected budgetlist with selected user (grants access to that list)
  -(/api/budgetlist/sharemany) shares selected budgetlist with selected users (grants access to that list)
  -(/api/budgetlist/revoke) revokes access to selected list from selected user
  -(/api/budgetlist/revokemany) revokes access to selected list from selected users
  
 Some endpoints are only available to admin users (for example getall endpoints)
  
Security is based on HTTP basic auth
List of endpoints is available after building project at swagger default local URL (http://localhost:8080/swagger-ui.html#)

Used Java Spring with lombok for easier data managment.
Data storage is handled by postgreSQL 12.2. '
Server runs on Heroku. 
