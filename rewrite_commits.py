# rewrite_commits.py
def callback(commit):
    commit.author_name = b"bawanibuddhini-ops"
    commit.author_email = b"bawanibuddhini@gmail.com"
    commit.committer_name = b"bawanibuddhini-ops"
    commit.committer_email = b"bawanibuddhini@gmail.com"
    
import git_filter_repo as gfr
gfr.RepoFilter(callback=callback).run()