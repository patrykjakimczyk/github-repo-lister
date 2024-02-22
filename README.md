# Github user's repository data fetcher - Atipera recruitment task

Application which uses [Github Rest API](https://docs.github.com/en/rest?apiVersion=2022-11-28) to fetch public repositories for a given username. For each repository, all its branches are retrieved along with the latest commit's sha. It may be required to add an access token header to the request if too many requests are made. This project was made as a recruitment task for Atipera.
## Used technologies
- Java 21
- Spring Boot 3.2.2
- JUnit 5
- Mockito
- Maven
## Requirements
- Java JDK 21
- Maven 3.6.3 or above
## How to run?
#### There are two options to run this application, but first you need to: 
Clone the repository in a desired directory
```bash
git clone https://github.com/patrykjakimczyk/github-repo-lister.git
```
### First option

Go to a project directory and run the application
```bash
mvn spring-boot:run
```
### Second option
Build .jar file with
```bash
mvn clean install
```
And navigate to a target folder and run .jar with 
```bash
java -jar <jarFilename.jar>
```
It runs on **localhost:8080**
## Endpoint
This application has only one endpoint:
`GET: /api/{user}/repos `
- where **{user}** must be replaced with a desired username
- Headers:
-- Accept (required) -  `application/json`
-- Authorization (optional) - for Github access token
- Parameters:
-- sort (optional): `created/updated/pushed/full_name`. Default value is `full_name`
-- direction (optional): `asc/desc`

If you specify **sort** value as **full_name** without specifying **direction** value, github API will sort in descending order. For other sorting values order will be ascending. More info [here](https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#list-repositories-for-a-user) .
## Example API call
```bash
curl -L \
  -H "Accept: application/json" \
  -H "Authorization: <access-token>" \
  localhost:8080/api/patrykjakimczyk/repos?sort=created&direction=desc
```

## Example responses
### Succesfull call
```bash
{
    "repositories": [
        {
            "repositoryName": "github-repo-lister",
            "ownerLogin": "patrykjakimczyk",
            "branches": [
                {
                    "branchName": "master",
                    "sha": "95f9a55fb1d4be8386771107c4d64c5c9045d7be"
                }
            ]
        }
    ]
}
```
### Call with non existing username
```bash
{
	"status": 404,
	"message": "User with provided username not found."
}
```
### Call with not acceptable 'Accept' header value
```bash
{
	"status": 406,
	"message": "Requested response's media type is not acceptable. Required type is 'application/json'."
}
```
